package davatar.wu.pendulumcoordinates;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

import javassist.CtMethod;

public class PendulumCoordinates implements WurmServerMod, Initable  {
    private static Logger logger = Logger.getLogger("PendulumCoordinates");
    
    public static void logException(String msg, Throwable e) {
        if (logger != null)
            logger.log(Level.SEVERE, msg, e);
    }

    public static void logInfo(String msg) {
        if (logger != null)
            logger.log(Level.INFO, msg);
    }

    public String getVersion() {
    	return "0.1";
    }

	@Override
	public void init() {
		try {
			logInfo("PendulumCoordinates is active.");

			logInfo("Overwriting Zones.getClosestSpring logic, to show coordinate based distance for pendulum instead of witchcraft.");
			CtMethod getClosestSpring = HookManager.getInstance().getClassPool().getMethod("com.wurmonline.server.zones.Zones", "getClosestSpring");
			getClosestSpring.setBody(
					"{        $1 = safeTileX($1);" +
					"        $2 = safeTileY($2);" +
					"        int closestX = -1;" +
					"        int closestY = -1;" +
					"        for (int x = safeTileX($1 - $3); x < safeTileX($1 + 1 + $3); ++x) {" +
					"            for (int y = safeTileY($2 - $3); y < safeTileY($2 + 1 + $3); ++y) {" +
					"                if (com.wurmonline.server.zones.Zone.hasSpring(x, y)) {" +
					"                    if (closestX < 0 || closestY < 0) {" +
					"                        closestX = ($1 - x);" +
					"                        closestY = ($2 - y);" +
					"                    }" +
					"                    else {" +
					"                        final int dx1 = $1 - x;" +
					"                        final int dy1 = $2 - y;" +
					"                        final int dx2 = closestX;" +
					"                        final int dy2 = closestY;" +
					"                        if (Math.sqrt((double)(dx1 * dx1 + dy1 * dy1)) < Math.sqrt((double)(dx2 * dx2 + dy2 * dy2))) {" +
					"                            closestX = (dx1);" +
					"                            closestY = (dy1);" +
					"                        }" +
					"                    }" +
					"                }" +
					"            }" +
					"        }" +
					"        return new int[] { closestX, closestY };" +
					"}");

			logInfo("Overwriting pendulum logic, to show coordinate based distance instead of witchcraft.");
			CtMethod locateSpring = HookManager.getInstance().getClassPool().getMethod("com.wurmonline.server.behaviours.Locates", "locateSpring");
			locateSpring.setBody(
					"{        final int[] closest = com.wurmonline.server.zones.Zones.getClosestSpring($1.getTileX(), $1.getTileY(), (int)(10.0f * getMaterialPendulumModifier($2.getMaterial())));" +
					"        final int west = $1.getTileX()-closest[0];" +
					"        final int north = $1.getTileY()-closest[1];" +
					"        String coordsText = \"(\" + (closest[0]>0?Integer.toString(closest[0]):Integer.toString(-closest[0]));" +
					"        coordsText += (closest[0]>0)?\"w\":\"e\";" +
					"        coordsText += \",\";" +
					"        coordsText += (closest[0]>0?Integer.toString(closest[1]):Integer.toString(-closest[1]));" +
					"        coordsText += (closest[1]>0)?\"n\":\"s\";" +
					"        coordsText += \")\";" +
					"        $1.getCommunicator().sendNormalServerMessage(\"You must go \" + coordsText + \" to reach the water source. The water source is at \" + west + \",\" + north + \", while you are at \" + $1.getTileX() + \",\" + $1.getTileY() + \".\");" +
					"}");
		} catch (Exception e) {
			logException("preInit: ", e);
			e.printStackTrace();
			throw new HookException(e);
		}
	}
}
