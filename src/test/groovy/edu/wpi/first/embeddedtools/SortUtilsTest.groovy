package edu.wpi.first.embeddedtools

import spock.lang.Specification

class SortUtilsTest extends Specification {

    def memberA = new SortUtils.TopoMember(name: "A", extra: 1)
    def memberB = new SortUtils.TopoMember(name: "B", dependsOn: ["A"], extra: 2)
    def memberC = new SortUtils.TopoMember(name: "C", extra: 3)
    def memberD = new SortUtils.TopoMember(name: "D", dependsOn: ["B", "C"], extra: 4)

    def allMembers = [ memberA, memberB, memberC, memberD ]

    def "sort dependencies"() {
        when:
        def sorted = SortUtils.topoSort(allMembers)

        then:
        sorted.indexOf(memberA.extra) < sorted.indexOf(memberB.extra)
        sorted.indexOf(memberB.extra) < sorted.indexOf(memberD.extra)
        sorted.indexOf(memberC.extra) < sorted.indexOf(memberD.extra)
    }

    def "cyclic"() {
        def member0 = new SortUtils.TopoMember(name: "0", dependsOn: ["1"])
        def member1 = new SortUtils.TopoMember(name: "1", dependsOn: ["0"])
        allMembers += [member0, member1]

        when:
        SortUtils.topoSort(allMembers)

        then:
        thrown SortUtils.CyclicDependencyException
    }

}
