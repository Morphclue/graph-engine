package uks.gmde2122;

import org.junit.Test;
import org.junit.runner.JUnitCore;

public class CertificateTest {
    public static void main(String[] args) {
        JUnitCore.main(CertificateTest.class.getName());
    }

    @Test
    public void graphSetup() {
        JGraph jGraph = new JGraph();
        JNode n1 = jGraph.createNode();
        JNode n2 = jGraph.createNode();
        JNode n3 = jGraph.createNode();
        JNode n4 = jGraph.createNode();

        jGraph.createEdge(n1, "e", n2);
        jGraph.createEdge(n2, "e", n3);
        jGraph.createEdge(n3, "e", n4);
        jGraph.createEdge(n4, "e", n1);

        String certificate = jGraph.computeCertificate();
        System.out.println("Certificate: \n" + certificate);
    }
}
