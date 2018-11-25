package im.conversations.compliance.utils;

import java.security.SecureRandom;

public class ConversationsUtils {

    private static final char[] VOWELS = "aeiou".toCharArray();
    private static final char[] CONSONANTS = "bcfghjklmnpqrstvwxyz".toCharArray();

    public static String generateConversationsLikePronounceableName() {
        final SecureRandom random = new SecureRandom();
        char[] output = new char[random.nextInt(4) * 2 + 5];
        boolean vowel = random.nextBoolean();
        for(int i = 0; i < output.length; ++i) {
            output[i] = vowel ? VOWELS[random.nextInt(VOWELS.length)] : CONSONANTS[random.nextInt(CONSONANTS.length)];
            vowel = !vowel;
        }
        return String.valueOf(output);
    }

}
