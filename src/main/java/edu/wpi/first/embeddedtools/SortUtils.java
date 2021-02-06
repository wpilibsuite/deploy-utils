package edu.wpi.first.embeddedtools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SortUtils {
    public static class TopoMember<T> {
        @SuppressWarnings("unchecked")
        public TopoMember(Map<String, Object> constructionMap) {
            if (constructionMap.containsKey("name")) {
                name = (String)constructionMap.get("name");
            }
            if (constructionMap.containsKey("dependsOn")) {
                dependsOn = (List<String>)constructionMap.get("dependsOn");
            }
            if (constructionMap.containsKey("extra")) {
                extra = (T)constructionMap.get("extra");
            }
        }

        public TopoMember(String name, List<String> dependsOn, T extra) {
            this.name = name;
            this.dependsOn = dependsOn;
            this.extra = extra;
        }

        public String name;
        public List<String> dependsOn = new ArrayList<>();
        public T extra = null;
        protected int mark = 0;
    }

    public static class CyclicDependencyException extends RuntimeException {
        private static final long serialVersionUID = 7116062129417645072L;

        public CyclicDependencyException(TopoMember<?> member) {
            super("Cyclic dependency! " + member.name + " : " + String.join(",", member.dependsOn));
        }
    }

    public static <T> List<T> topoSort(List<TopoMember<T>> members) {

        List<TopoMember<T>> unmarked = new ArrayList<>(members);
        List<T> sorted = new ArrayList<>(unmarked.size());
        while (true) {
            boolean found = false;
            for (TopoMember<T> member : unmarked) {
                if (member.mark == 0) {
                    visit(member, members, sorted);
                    unmarked.remove(member);
                    found = true;
                    break;
                }
            }
            if (!found) {
                break;
            }
        }
        return sorted;
    }

    private static <T> void visit(TopoMember<T> member, List<TopoMember<T>> members, List<T> sorted) {
        if (member.mark == 1) throw new CyclicDependencyException(member); // Temp mark
        if (member.mark == 2) return; // Perm mark
        member.mark = 1;
        for (String dep : member.dependsOn) {
            for (TopoMember<T> mem : members) {
                if (mem.name.equals((dep))) {
                    visit(mem, members, sorted);
                    break;
                }
            }
        }
        member.mark = 2;
        sorted.add(member.extra);
    }
}
