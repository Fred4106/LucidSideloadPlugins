
import scala.util.chaining.scalaUtilChainingOps

object Test extends App {
	import com.fredplugins.pvmHelper2.gauntlet.values
	println(values.map(v => v.name ->  v.ids).mkString("\n"))
//	new PvmGui().tap(gui => {
//		new Thread(() => {
//			Thread.sleep(20000)
//			gui.closeOperation()
//		})
//	})
//
//	source.push(new SNpcSpawned(22, 224, 33, null))
//	println("test")
//	for (i <- 1 to 250) {
//		Thread.sleep(100)
//		source.push(new SNpcSpawned((i * 7), i, 33 + i * 13 % 7, null))
//	}
}
