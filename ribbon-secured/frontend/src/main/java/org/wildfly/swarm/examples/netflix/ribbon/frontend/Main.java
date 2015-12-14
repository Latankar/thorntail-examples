package org.wildfly.swarm.examples.netflix.ribbon.frontend;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.jgroups.JGroupsFraction;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Lance Ball
 */
public class Main {
    public static void main(String... args) throws Exception {
        Container container = new Container();
        JGroupsFraction fraction = new JGroupsFraction()
                .defaultChannel( "swarm-jgroups")
                .stack( "udp", (s)->{
                    s.transport( "UDP", (t)->{
                        t.socketBinding("jgroups-udp");
                    });
                    s.protocol("TCP", (p -> {
                        p.property("bind_port", "9090");
                    }));
                    s.protocol("TCPPING", (p)-> {
                        p.property("initial_hosts", "localhost[9090],localhost[9091],localhost[9092],localhost[9093]")
                                .property("port_range", "4")
                                .property("timeout", "3000")
                                .property("num_initial_members", "4");
                    });
                    s.protocol( "FD_SOCK", (p)->{
                        p.socketBinding( "jgroups-udp-fd" );
                    });
                    s.protocol( "FD_ALL" );
                    s.protocol( "VERIFY_SUSPECT" );
                    s.protocol( "pbcast.NAKACK2" );
                    s.protocol( "UNICAST3" );
                    s.protocol( "pbcast.STABLE" );
                    s.protocol( "pbcast.GMS" );
                    s.protocol( "UFC" );
                    s.protocol( "MFC" );
                    s.protocol( "FRAG2" );
                    s.protocol( "RSVP" );
                })
                .channel( "swarm-jgroups", (c)->{
                    c.stack( "udp" );
                });
        container.fraction(fraction);
        container.start();
        WARArchive war = ShrinkWrap.create(WARArchive.class, "frontend.war");
        war.staticContent();
        war.addAllDependencies();
        container.deploy(war);
    }
}