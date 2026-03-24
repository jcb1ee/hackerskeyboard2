/*
 * Copyright (C) 2026 Hacker's Keyboard 2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.jcb1ee.hackerskeyboard2;

/**
 * Implements Hangul (Korean) syllable composition for the Dubeolsik (두벌식) layout.
 *
 * <p>The Korean Hangul syllable composition algorithm uses the Unicode standard formula:
 * syllable = 0xAC00 + (choseongIndex * 21 + jungseongIndex) * 28 + jongseongIndex
 *
 * <p>Inputs are expected as Hangul Compatibility Jamo (U+3131–U+3163).
 * Each call to {@link #compose} returns the text to commit (completed syllable) and
 * the new composing text to show in the input field.
 */
public class HangulComposer {

    /** Hangul syllable block base codepoint (가). */
    private static final int HANGUL_BASE = 0xAC00;
    private static final int CHOSEONG_COUNT = 19;
    private static final int JUNGSEONG_COUNT = 21;
    private static final int JONGSEONG_COUNT = 28;

    // ------------------------------------------------------------------ data tables

    /**
     * Maps a Compatibility Jamo consonant codepoint to its choseong (initial) index (0–18).
     * Returns -1 if not a valid initial consonant (e.g. tense consonants ㄸ, ㅃ, ㅉ are valid choseong
     * but NOT valid jongseong).
     */
    private static int compatConsonantToChoseong(int code) {
        switch (code) {
            case 0x3131: return 0;  // ㄱ
            case 0x3132: return 1;  // ㄲ
            case 0x3134: return 2;  // ㄴ
            case 0x3137: return 3;  // ㄷ
            case 0x3138: return 4;  // ㄸ
            case 0x3139: return 5;  // ㄹ
            case 0x3141: return 6;  // ㅁ
            case 0x3142: return 7;  // ㅂ
            case 0x3143: return 8;  // ㅃ
            case 0x3145: return 9;  // ㅅ
            case 0x3146: return 10; // ㅆ
            case 0x3147: return 11; // ㅇ
            case 0x3148: return 12; // ㅈ
            case 0x3149: return 13; // ㅉ
            case 0x314A: return 14; // ㅊ
            case 0x314B: return 15; // ㅋ
            case 0x314C: return 16; // ㅌ
            case 0x314D: return 17; // ㅍ
            case 0x314E: return 18; // ㅎ
            default:     return -1;
        }
    }

    /**
     * Maps a Compatibility Jamo vowel codepoint (U+314F–U+3163) to its jungseong index (0–20).
     * Returns -1 if not a vowel.
     */
    private static int compatVowelToJungseong(int code) {
        if (code >= 0x314F && code <= 0x3163) {
            return code - 0x314F; // direct mapping: 21 vowels
        }
        return -1;
    }

    /**
     * Maps a Compatibility Jamo consonant to its jongseong (final) index (1–27).
     * Returns 0 if not a valid jongseong (e.g. tense consonants ㄸ=0x3138, ㅃ=0x3143, ㅉ=0x3149
     * cannot appear as final consonants in standard Korean).
     */
    private static int compatConsonantToJongseong(int code) {
        switch (code) {
            case 0x3131: return 1;  // ㄱ
            case 0x3132: return 2;  // ㄲ
            case 0x3134: return 4;  // ㄴ
            case 0x3137: return 7;  // ㄷ
            case 0x3139: return 8;  // ㄹ
            case 0x3141: return 16; // ㅁ
            case 0x3142: return 17; // ㅂ
            case 0x3145: return 19; // ㅅ
            case 0x3146: return 20; // ㅆ
            case 0x3147: return 21; // ㅇ
            case 0x3148: return 22; // ㅈ
            case 0x314A: return 23; // ㅊ
            case 0x314B: return 24; // ㅋ
            case 0x314C: return 25; // ㅌ
            case 0x314D: return 26; // ㅍ
            case 0x314E: return 27; // ㅎ
            // ㄸ(0x3138), ㅃ(0x3143), ㅉ(0x3149) are NOT valid jongseong
            default:     return 0;
        }
    }

    /**
     * Maps a choseong index back to its Compatibility Jamo codepoint for display.
     * Used when a syllable is in the "single initial consonant" composing state.
     */
    private static char choseongToCompatJamo(int choseongIdx) {
        final int[] TABLE = {
            0x3131, // 0  ㄱ
            0x3132, // 1  ㄲ
            0x3134, // 2  ㄴ
            0x3137, // 3  ㄷ
            0x3138, // 4  ㄸ
            0x3139, // 5  ㄹ
            0x3141, // 6  ㅁ
            0x3142, // 7  ㅂ
            0x3143, // 8  ㅃ
            0x3145, // 9  ㅅ
            0x3146, // 10 ㅆ
            0x3147, // 11 ㅇ
            0x3148, // 12 ㅈ
            0x3149, // 13 ㅉ
            0x314A, // 14 ㅊ
            0x314B, // 15 ㅋ
            0x314C, // 16 ㅌ
            0x314D, // 17 ㅍ
            0x314E, // 18 ㅎ
        };
        if (choseongIdx >= 0 && choseongIdx < TABLE.length) {
            return (char) TABLE[choseongIdx];
        }
        return 0;
    }

    /**
     * Tries to combine two consecutive vowel jungseong indices into a compound vowel.
     * @return compound jungseong index, or -1 if not combinable
     */
    private static int tryCompoundVowel(int v1, int v2) {
        // v1, v2 are jungseong indices (0–20)
        if (v1 == 8  && v2 == 0)  return 9;  // ㅗ+ㅏ=ㅘ
        if (v1 == 8  && v2 == 1)  return 10; // ㅗ+ㅐ=ㅙ
        if (v1 == 8  && v2 == 20) return 11; // ㅗ+ㅣ=ㅚ
        if (v1 == 13 && v2 == 4)  return 14; // ㅜ+ㅓ=ㅝ
        if (v1 == 13 && v2 == 5)  return 15; // ㅜ+ㅔ=ㅞ
        if (v1 == 13 && v2 == 20) return 16; // ㅜ+ㅣ=ㅟ
        if (v1 == 18 && v2 == 20) return 19; // ㅡ+ㅣ=ㅢ
        return -1;
    }

    /**
     * For a compound vowel jungseong index, returns the first component vowel index.
     * Returns -1 if not compound.
     */
    private static int decomposeVowelFirst(int vIdx) {
        switch (vIdx) {
            case 9:  case 10: case 11: return 8;  // ㅘ/ㅙ/ㅚ → ㅗ
            case 14: case 15: case 16: return 13; // ㅝ/ㅞ/ㅟ → ㅜ
            case 19: return 18; // ㅢ → ㅡ
            default: return -1;
        }
    }

    /**
     * Tries to form a compound jongseong from a current jongseong index plus a new consonant.
     * @param jong1   current jongseong index
     * @param newCode new compat jamo consonant code
     * @return compound jongseong index, or 0 if not combinable
     */
    private static int tryCompoundJongseong(int jong1, int newCode) {
        if (jong1 == 1  && newCode == 0x3145) return 3;  // ㄱ+ㅅ=ㄳ
        if (jong1 == 4  && newCode == 0x3148) return 5;  // ㄴ+ㅈ=ㄵ
        if (jong1 == 4  && newCode == 0x314E) return 6;  // ㄴ+ㅎ=ㄶ
        if (jong1 == 8  && newCode == 0x3131) return 9;  // ㄹ+ㄱ=ㄺ
        if (jong1 == 8  && newCode == 0x3141) return 10; // ㄹ+ㅁ=ㄻ
        if (jong1 == 8  && newCode == 0x3142) return 11; // ㄹ+ㅂ=ㄼ
        if (jong1 == 8  && newCode == 0x3145) return 12; // ㄹ+ㅅ=ㄽ
        if (jong1 == 8  && newCode == 0x314C) return 13; // ㄹ+ㅌ=ㄾ
        if (jong1 == 8  && newCode == 0x314D) return 14; // ㄹ+ㅍ=ㄿ
        if (jong1 == 8  && newCode == 0x314E) return 15; // ㄹ+ㅎ=ㅀ
        if (jong1 == 17 && newCode == 0x3145) return 18; // ㅂ+ㅅ=ㅄ
        return 0;
    }

    /**
     * For a compound jongseong index, returns [first_jongseong_index, second_choseong_index].
     * Used when a vowel follows a syllable with a compound final consonant — the compound
     * splits: first part stays as jongseong, second part becomes the initial of the new syllable.
     */
    private static int[] splitCompoundJongseong(int compound) {
        switch (compound) {
            case 3:  return new int[]{1,  9};  // ㄳ → ㄱ(jong=1),  ㅅ(cho=9)
            case 5:  return new int[]{4,  12}; // ㄵ → ㄴ(jong=4),  ㅈ(cho=12)
            case 6:  return new int[]{4,  18}; // ㄶ → ㄴ(jong=4),  ㅎ(cho=18)
            case 9:  return new int[]{8,  0};  // ㄺ → ㄹ(jong=8),  ㄱ(cho=0)
            case 10: return new int[]{8,  6};  // ㄻ → ㄹ(jong=8),  ㅁ(cho=6)
            case 11: return new int[]{8,  7};  // ㄼ → ㄹ(jong=8),  ㅂ(cho=7)
            case 12: return new int[]{8,  9};  // ㄽ → ㄹ(jong=8),  ㅅ(cho=9)
            case 13: return new int[]{8,  16}; // ㄾ → ㄹ(jong=8),  ㅌ(cho=16)
            case 14: return new int[]{8,  17}; // ㄿ → ㄹ(jong=8),  ㅍ(cho=17)
            case 15: return new int[]{8,  18}; // ㅀ → ㄹ(jong=8),  ㅎ(cho=18)
            case 18: return new int[]{17, 9};  // ㅄ → ㅂ(jong=17), ㅅ(cho=9)
            default: return null;
        }
    }

    /** Returns true if the jongseong index represents a compound final consonant. */
    private static boolean isCompoundJongseong(int jong) {
        return jong == 3 || jong == 5 || jong == 6
            || (jong >= 9 && jong <= 15) || jong == 18;
    }

    /**
     * Maps a single jongseong index to its corresponding choseong index, used when
     * the final consonant of a syllable must become the initial of the next syllable.
     */
    private static int jongseongToChoseong(int jong) {
        switch (jong) {
            case 1:  return 0;  // ㄱ
            case 2:  return 1;  // ㄲ
            case 4:  return 2;  // ㄴ
            case 7:  return 3;  // ㄷ
            case 8:  return 5;  // ㄹ
            case 16: return 6;  // ㅁ
            case 17: return 7;  // ㅂ
            case 19: return 9;  // ㅅ
            case 20: return 10; // ㅆ
            case 21: return 11; // ㅇ
            case 22: return 12; // ㅈ
            case 23: return 14; // ㅊ
            case 24: return 15; // ㅋ
            case 25: return 16; // ㅌ
            case 26: return 17; // ㅍ
            case 27: return 18; // ㅎ
            default: return -1;
        }
    }

    // ------------------------------------------------------------------ state

    /** Choseong (initial consonant) index 0–18, or -1 if none. */
    private int mChoseong = -1;

    /** Jungseong (vowel) index 0–20, or -1 if none. */
    private int mJungseong = -1;

    /**
     * Jongseong (final consonant) index 1–27, or 0/negative if none.
     * Compound values (3, 5, 6, 9–15, 18) represent cluster finals.
     */
    private int mJongseong = 0;

    // ------------------------------------------------------------------ public API

    /** Returns true if there is an active composition in progress. */
    public boolean isComposing() {
        return mChoseong >= 0;
    }

    /** Returns the current composing string to show via setComposingText. */
    public String getComposing() {
        if (mChoseong < 0) return "";
        if (mJungseong < 0) {
            // Only initial consonant — display as standalone compat jamo
            return String.valueOf(choseongToCompatJamo(mChoseong));
        }
        // Full or partial syllable block
        int syllable = HANGUL_BASE + (mChoseong * JUNGSEONG_COUNT + mJungseong) * JONGSEONG_COUNT
                + (mJongseong > 0 ? mJongseong : 0);
        return String.valueOf((char) syllable);
    }

    /** Resets composition state without committing anything. */
    public void reset() {
        mChoseong = -1;
        mJungseong = -1;
        mJongseong = 0;
    }

    /**
     * Processes a Hangul Compatibility Jamo keypress.
     *
     * @param code Compatibility Jamo codepoint (U+3131–U+3163)
     * @return Result containing text to commit (may be empty) and new composing text
     */
    public Result compose(int code) {
        int vowelIdx = compatVowelToJungseong(code);
        if (vowelIdx >= 0) {
            return processVowel(vowelIdx);
        }
        int choIdx = compatConsonantToChoseong(code);
        if (choIdx >= 0) {
            return processConsonant(code, choIdx);
        }
        // Unknown jamo — commit current composition, pass char through as-is
        String toCommit = getComposing();
        reset();
        return new Result(toCommit, String.valueOf((char) code));
    }

    /**
     * Handles a backspace while composing.
     *
     * @return new composing text after the backspace. An empty string means the composition
     *         was cleared (no preceding character should be deleted — the composed jamo was
     *         only in composing state). Returns null only when not composing (caller should
     *         check {@link #isComposing()} first).
     */
    public String backspace() {
        if (mChoseong < 0) return null; // not composing

        if (mJongseong > 0) {
            if (isCompoundJongseong(mJongseong)) {
                // Undo second half of compound final: e.g. ㄺ → ㄹ
                mJongseong = splitCompoundJongseong(mJongseong)[0];
            } else {
                // Remove simple final consonant: go back to CV
                mJongseong = 0;
            }
            return getComposing();
        }

        if (mJungseong >= 0) {
            int firstVowel = decomposeVowelFirst(mJungseong);
            if (firstVowel >= 0) {
                // Decompose compound vowel: e.g. ㅘ → ㅗ
                mJungseong = firstVowel;
            } else {
                // Remove simple vowel: back to lone initial consonant
                mJungseong = -1;
            }
            return getComposing();
        }

        // Only choseong remaining — clear composition entirely
        reset();
        return ""; // empty = finish composing, no real delete
    }

    // ------------------------------------------------------------------ private helpers

    private Result processVowel(int vIdx) {
        if (mChoseong < 0) {
            // No active composition: vowel alone uses ㅇ (silent initial, index 11)
            mChoseong = 11;
            mJungseong = vIdx;
            mJongseong = 0;
            return new Result("", getComposing());
        }

        if (mJungseong < 0) {
            // C + V: combine into first syllable block
            mJungseong = vIdx;
            mJongseong = 0;
            return new Result("", getComposing());
        }

        if (mJongseong <= 0) {
            // CV + V: try compound vowel (e.g. ㅗ + ㅏ = ㅘ)
            int compound = tryCompoundVowel(mJungseong, vIdx);
            if (compound >= 0) {
                mJungseong = compound;
                return new Result("", getComposing());
            }
            // Not combinable: commit CV with ㅇ initial kept, start new syllable with ㅇ + V
            String committed = getComposing();
            mChoseong = 11; // ㅇ
            mJungseong = vIdx;
            mJongseong = 0;
            return new Result(committed, getComposing());
        }

        // CVC + V: jongseong moves to become initial of new syllable
        int[] split = splitCompoundJongseong(mJongseong);
        String committed;
        if (split != null) {
            // Compound jongseong: first part stays as final, second part becomes new initial
            committed = syllableString(mChoseong, mJungseong, split[0]);
            mChoseong = split[1];
        } else {
            // Simple jongseong: commit CV (no final), jongseong becomes new initial
            committed = syllableString(mChoseong, mJungseong, 0);
            mChoseong = jongseongToChoseong(mJongseong);
            if (mChoseong < 0) mChoseong = 11; // fallback: ㅇ
        }
        mJungseong = vIdx;
        mJongseong = 0;
        return new Result(committed, getComposing());
    }

    private Result processConsonant(int code, int choIdx) {
        if (mChoseong < 0) {
            // EMPTY: start new composition with this initial
            mChoseong = choIdx;
            mJungseong = -1;
            mJongseong = 0;
            return new Result("", getComposing());
        }

        if (mJungseong < 0) {
            // C + C: commit first consonant, start new composition
            String committed = getComposing();
            mChoseong = choIdx;
            mJungseong = -1;
            mJongseong = 0;
            return new Result(committed, getComposing());
        }

        if (mJongseong <= 0) {
            // CV + C: try adding as jongseong
            int jong = compatConsonantToJongseong(code);
            if (jong > 0) {
                mJongseong = jong;
                return new Result("", getComposing());
            }
            // Not a valid jongseong (e.g. tense ㄸ, ㅃ, ㅉ): commit CV, start new C
            String committed = getComposing();
            mChoseong = choIdx;
            mJungseong = -1;
            mJongseong = 0;
            return new Result(committed, getComposing());
        }

        // CVC + C: try compound jongseong
        int compound = tryCompoundJongseong(mJongseong, code);
        if (compound > 0) {
            mJongseong = compound;
            return new Result("", getComposing());
        }

        // Commit CVC, start new composition with this consonant
        String committed = getComposing();
        mChoseong = choIdx;
        mJungseong = -1;
        mJongseong = 0;
        return new Result(committed, getComposing());
    }

    private static String syllableString(int cho, int jung, int jong) {
        int syllable = HANGUL_BASE + (cho * JUNGSEONG_COUNT + jung) * JONGSEONG_COUNT + jong;
        return String.valueOf((char) syllable);
    }

    // ------------------------------------------------------------------ Result

    /** Result of a {@link #compose} call: text to commit + new composing text. */
    public static class Result {
        /** Text to commit to the editor immediately (may be empty, never null). */
        public final String commit;
        /** New composing text to show via setComposingText (may be empty). */
        public final String composing;

        Result(String commit, String composing) {
            this.commit = commit;
            this.composing = composing;
        }
    }
}
