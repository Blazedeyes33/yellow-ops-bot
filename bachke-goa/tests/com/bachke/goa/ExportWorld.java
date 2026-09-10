package com.bachke.goa;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Desktop exporter: writes World3D route meshes and the CP9 actor mesh (hazards/coins) as raw float
 *  streams (10 floats per vertex: pos3 normal3 color3 wind1) for the three.js preview harness. */
public final class ExportWorld {
    public static void main(String[] args) throws Exception {
        String outDir = args[0];
        for (int route = 0; route < 5; route++) {
            Mesh3D m = World3D.build(route);
            write(outDir + "/world" + route + ".f32", m);
        }
        // a representative street scene: runner at t=25s with hazards
        RunnerCore core = new RunnerCore(); core.streetMode = true; core.start(4);
        for (int i = 0; i < (int) (25 / RunnerCore.STEP); i++) core.advance(RunnerCore.STEP);
        Stage3D stage = new Stage3D(); stage.skinnedCharacter = true;
        stage.frame(core, 0, 25.0, false);
        write(outDir + "/actors.f32", stage.actors);
        try (DataOutputStream d = new DataOutputStream(new FileOutputStream(outDir + "/state.txt"))) {
            d.writeBytes("renderX " + core.renderX() + "\nrenderY " + core.renderY() + "\ndistance " + core.renderDistance() + "\nroute " + core.route() + "\nspeed " + core.speed() + "\n");
        }
        float[] cam = Camera3D.matrix(1080.0 / 2400.0, false);
        try (DataOutputStream d = new DataOutputStream(new FileOutputStream(outDir + "/camera.f32"))) {
            ByteBuffer b = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN); for (float f : cam) b.putFloat(f); d.write(b.array());
        }
        System.out.println("exported 5 worlds + actors; distance=" + core.renderDistance() + " x=" + core.renderX());
    }
    static void write(String path, Mesh3D m) throws Exception {
        ByteBuffer b = ByteBuffer.allocate(m.size * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < m.size; i++) b.putFloat(m.data[i]);
        try (FileOutputStream f = new FileOutputStream(path)) { f.write(b.array()); }
        System.out.println(path + " vertices=" + m.vertices());
    }
}
