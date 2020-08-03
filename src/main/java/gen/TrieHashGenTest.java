package gen;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TrieHashGenTest {
    List<String> asList(String... str) {
        return new ArrayList<>(Arrays.asList(str));
    }

    @Test
    public void testTrieHashGen() {
        TrieHashGen trie = new TrieHashGen(asList("ma", "man", "mad", "manure"));
        String result = trie.generate();
        System.out.println(result);
    }
}
