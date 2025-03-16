package com.fredplugins.kroovy.jfx.editor

import java.io.{DataInputStream, DataOutputStream, IOException, StringReader}
import java.time.Duration
import java.util
import java.util.Collections
import java.util.regex.Pattern

import groovyjarjarantlr4.v4.runtime.{CharStreams, CommonTokenStream, LexerNoViableAltException, Token}
import javafx.application.Platform
import javafx.beans.NamedArg
import javafx.scene.control.{Button, Label, TreeView}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.text.TextFlow
import com.fredplugins.kroovy.jfx.editor.KroovyArea.whiteSpacePattern
import javafx.stage.Popup
import org.apache.groovy.parser.antlr4.GroovyLangLexer
import org.fxmisc.richtext.{StyledTextArea, TextExt}
import org.fxmisc.richtext.model.{Codec, EditableStyledDocument, PlainTextChange, SimpleEditableStyledDocument, StyleSpansBuilder}
import java.time.Duration

import javafx.event.ActionEvent
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.geometry.{HPos, Orientation}
import javafx.scene.layout.{FlowPane, HBox, Priority, VBox}
import com.fredplugins.kroovy.jfx.autocomplete.KroovyEditorDemo.sampleScript
import com.fredplugins.kroovy.jfx.autocomplete.KroovyParser
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.event.MouseOverTextEvent
import org.fxmisc.richtext.model.TwoDimensional.Bias
//import tornadofx.FX

import scala.jdk.CollectionConverters._

object KroovyArea {
	val whiteSpacePattern: Pattern = Pattern.compile("^\\s+")

	val FRAG_CODEC: Codec[KroovyTokenStyle] = new Codec[KroovyTokenStyle]() {
		override def getName = "kroovyTokenStyle"
		@throws[IOException]
		override def encode(os: DataOutputStream, s: KroovyTokenStyle): Unit = {
			os.writeInt(s.tokenType)
		}
		@throws[IOException]
		override def decode(is: DataInputStream): KroovyTokenStyle = {
			new KroovyTokenStyle(is.readInt())
		}
	}
}

class BetterKroovyArea() extends VBox() {
	val toolbar = new FlowPane(Orientation.HORIZONTAL)
	toolbar.setColumnHalignment(HPos.LEFT); // align labels on left
	toolbar.setPrefWrapLength(400); // preferred width = 200
	val refreshButton = new Button("Refresh")
	toolbar.getChildren.add(refreshButton)

	val codeArea = new KroovyArea()
	val scrolledCodeArea = new VirtualizedScrollPane[KroovyArea](codeArea)
//
//	def openAstViewer(nodes: util.List[ASTNode]): Unit = {
//		nodes.asScala.find {
//			case x: BlockStatement => true
//		}.map(_.asInstanceOf[BlockStatement]).foreach(node => AstViewerUtil.INSTANCE.open(node))
//		//		find<AstViewer>(mapOf(CustomerEditor::customer to customer)).openWindow()
//	}

//	refreshButton.setOnAction((ae: ActionEvent) => {
//		openAstViewer(KroovyParser.getAst(codeArea.getText))
//	})
	val rootItem = new TreeItem[String]("Inbox")
	rootItem.setExpanded(true)
	for (i <- 1 until 6) {
		val item = new TreeItem[String]("Message" + i)
		rootItem.getChildren.add(item)
	}

	//	val astArea: TreeView[String] = new TreeView[String](rootItem)
	val body: HBox = new HBox()
	body.getChildren.addAll(/*astArea, */scrolledCodeArea)
	HBox.setHgrow(scrolledCodeArea, Priority.ALWAYS)

	getChildren.addAll(toolbar, body)
	VBox.setVgrow(toolbar, Priority.NEVER)
	VBox.setVgrow(body, Priority.ALWAYS)
}

object BetterKroovyArea {
	def apply(source: String): BetterKroovyArea = {
		val area = new BetterKroovyArea()
		area.codeArea.appendText(source)
		area.codeArea.getUndoManager.forgetHistory()
		area.codeArea.getUndoManager.mark()

		// position the caret at the beginning
		area.codeArea.selectRange(0, 0)
		area
	}
}

//class KroovyArea2(@NamedArg ("document") document: EditableStyledDocument[util.Collection[String], String, KroovyTokenStyle], @NamedArg ("preserveStyle") preserveStyle: Boolean) extends KroovyAreaInf[KroovyTokenStyle](new KroovyTokenStyle(0), document, preserveStyle) {
//	{
//		setStyleCodecs (Codec.collectionCodec (Codec.STRING_CODEC), Codec.styledTextCodec (KroovyArea.FRAG_CODEC ) )
//		getStyleClass.add("kroovy-area")
//		// load the default style that defines a fixed-width font
//		getStylesheets.add(classOf[KroovyArea].getResource("kroovy-area.css").toExternalForm)
//
//		// don't apply preceding style to typed text
//		setUseInitialStyleForInsertion(true)
//		addEventHandler(KeyEvent.KEY_PRESSED, (KE: KeyEvent) => {
//			if (KE.getCode eq KeyCode.ENTER) {
//				val caretPosition = getCaretPosition
//				val currentParagraph = getCurrentParagraph
//				val m0 = whiteSpacePattern.matcher(getParagraph(currentParagraph - 1).getSegments.get(0))
//				if (m0.find) Platform.runLater(() => insertText(caretPosition, m0.group))
//			}
//		})
//
//		def openAstViewer(nodes: util.List[ASTNode]): Unit = {
//			nodes.asScala.find {
//				case x: BlockStatement => true
//			}.map(_.asInstanceOf[BlockStatement]).foreach(node => AstViewerUtil.INSTANCE.open(node))
//			//		find<AstViewer>(mapOf(CustomerEditor::customer to customer)).openWindow()
//		}
//
//		refreshButton.setOnAction((ae: ActionEvent) => {
//			openAstViewer(KroovyParser.getAst(codeArea.getText))
//		})
//
//		val popup = new Popup
//		val popupMsg = new Label()
//		popupMsg.setStyle("-fx-background-color: black;" + "-fx-text-fill: white;" + "-fx-padding: 5;")
//		popup.getContent.add(popupMsg)
//
//		this.setMouseOverTextDelay(Duration.ofSeconds(1))
//		this.addEventHandler[MouseOverTextEvent](MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e => {
//			val chIdx = e.getCharacterIndex
//			val pos = e.getScreenPosition
//			val tPos = this.offsetToPosition(chIdx, Bias.Forward)
//			val rng = this.getStyleRangeAtPosition(tPos.getMajor, tPos.getMinor)
//			popupMsg.setText(s"Token '${this.getText(tPos.getMajor).substring(rng.getStart, rng.getEnd)}' is type '${this.getStyleAtPosition(tPos.getMajor, tPos.getMinor).getTokenTypeName}'")
//			popup.show(this, pos.getX, pos.getY + 10)
//		})
//		this.addEventHandler[MouseOverTextEvent](MouseOverTextEvent.MOUSE_OVER_TEXT_END, (e) => {
//			popup.hide()
//		})
//		computeHighlighting()
//	}
//	val textChangeSubscription = plainTextChanges.subscribe((tc: PlainTextChange) => {
//		val removed = tc.getRemoved
//		val inserted = tc.getInserted
//		val position = tc.getPosition
//		if (!removed.isEmpty && inserted.isEmpty) {
//			// deletion
//		}
//		else if (!inserted.isEmpty && removed.isEmpty) {
//			// insertion
//		}
//		else {
//			// replacement
//		}
//	})
//
//	val recomputeCST = multiPlainChanges.successionEnds(Duration.ofMillis(500)).subscribe((change: util.List[PlainTextChange]) => {
//		this.computeHighlighting()
//	})
//
//	def this (@NamedArg ("preserveStyle") preserveStyle: Boolean) = {
//		this (new SimpleEditableStyledDocument[util.Collection[String], KroovyTokenStyle] (Collections.emptyList[String], new KroovyTokenStyle(0)), preserveStyle)
//	}
//
//	/**
//	 * Creates a text area with empty text content.
//	 */
//	def this() = {
//		this (true)
//	}
//
//	def this(text: String) = {
//		this()
//		appendText(text)
//		getUndoManager.forgetHistory()
//		getUndoManager.mark()
//
//		// position the caret at the beginning
//		selectRange(0, 0)
//	}
//
//	private def computeHighlighting(): Unit = {
//		val t = getText()
//		val charStream = CharStreams.fromReader(new StringReader(t))
//		val lexer: GroovyLangLexer = new GroovyLangLexer(charStream)
//		val tokensStream: CommonTokenStream = new CommonTokenStream(lexer)
//		val spansBuilder = new StyleSpansBuilder[KroovyTokenStyle]()
//		try {
//			tokensStream.fill()
//			import scala.jdk.CollectionConverters._
//			val tokens = tokensStream.getTokens.asScala.toList
//			spansBuilder.add(new KroovyTokenStyle(0), tokens.headOption.map(_.getStartIndex).getOrElse(t.length))
//			for(i <- tokens.indices) {
//				val last: Token = if(i > 0) tokens(i-1) else null
//				val tkn = tokens(i)
//				if(last != null) spansBuilder.add(new KroovyTokenStyle(0), tkn.getStartIndex - (last.getStopIndex+1))
//				spansBuilder.add(KroovyTokenStyle(tkn), (tkn.getStopIndex+1)-tkn.getStartIndex)
//			}
//			spansBuilder.add(new KroovyTokenStyle(0), tokens.lastOption.map(f => t.length - (f.getStopIndex+1)).getOrElse(0))
//		}
//		catch {
//			case exception: LexerNoViableAltException =>
//				exception.printStackTrace()
//				spansBuilder.add(new KroovyTokenStyle(0), t.length)
//		}
//		Platform.runLater(() => setStyleSpans(0, spansBuilder.create()))
//	}
//}

class KroovyArea(@NamedArg ("document") document: EditableStyledDocument[util.Collection[String], String, KroovyTokenStyle], @NamedArg ("preserveStyle") preserveStyle: Boolean)
	extends StyledTextArea[util.Collection[String], KroovyTokenStyle](Collections.emptyList[String],
		(paragraph: TextFlow, styleClasses: util.Collection[String] ) => paragraph.getStyleClass.addAll(styleClasses),
		new KroovyTokenStyle(0),
		(text: TextExt, tokenStyle) => {
			Option(tokenStyle).foreach(k => text.getStyleClass.add(k.toStyle))
		},
		document,
		preserveStyle) {
	{
		setStyleCodecs (Codec.collectionCodec (Codec.STRING_CODEC), Codec.styledTextCodec (KroovyArea.FRAG_CODEC ) )
		getStyleClass.add("kroovy-area")
		// load the default style that defines a fixed-width font
		getStylesheets.add(classOf[KroovyArea].getResource("kroovy-area.css").toExternalForm)

		// don't apply preceding style to typed text
		setUseInitialStyleForInsertion(true)
		addEventHandler(KeyEvent.KEY_PRESSED, (KE: KeyEvent) => {
			if (KE.getCode eq KeyCode.ENTER) {
				val caretPosition = getCaretPosition
				val currentParagraph = getCurrentParagraph
				val m0 = whiteSpacePattern.matcher(getParagraph(currentParagraph - 1).getSegments.get(0))
				if (m0.find) Platform.runLater(() => insertText(caretPosition, m0.group))
			}
		})
		val popup = new Popup
		val popupMsg = new Label()
		popupMsg.setStyle("-fx-background-color: black;" + "-fx-text-fill: white;" + "-fx-padding: 5;")
		popup.getContent.add(popupMsg)

		this.setMouseOverTextDelay(Duration.ofSeconds(1))
		this.addEventHandler[MouseOverTextEvent](MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e => {
			val chIdx = e.getCharacterIndex
			val pos = e.getScreenPosition
			val tPos = this.offsetToPosition(chIdx, Bias.Forward)
			val rng = this.getStyleRangeAtPosition(tPos.getMajor, tPos.getMinor)
			popupMsg.setText(s"Token '${this.getText(tPos.getMajor).substring(rng.getStart, rng.getEnd)}' is type '${this.getStyleAtPosition(tPos.getMajor, tPos.getMinor).getTokenTypeName}'")
			popup.show(this, pos.getX, pos.getY + 10)
		})
		this.addEventHandler[MouseOverTextEvent](MouseOverTextEvent.MOUSE_OVER_TEXT_END, (e) => {
			popup.hide()
		})
		computeHighlighting()
	}
	val textChangeSubscription = plainTextChanges.subscribe((tc: PlainTextChange) => {
		val removed = tc.getRemoved
		val inserted = tc.getInserted
		val position = tc.getPosition
		if (!removed.isEmpty && inserted.isEmpty) {
			// deletion
		}
		else if (!inserted.isEmpty && removed.isEmpty) {
			// insertion
		}
		else {
			// replacement
		}
	})

	val recomputeCST = multiPlainChanges.successionEnds(Duration.ofMillis(500)).subscribe((change: util.List[PlainTextChange]) => {
		this.computeHighlighting()
	})

	def this (@NamedArg ("preserveStyle") preserveStyle: Boolean) = {
		this (new SimpleEditableStyledDocument[util.Collection[String], KroovyTokenStyle] (Collections.emptyList[String], new KroovyTokenStyle(0)), preserveStyle)
	}

	/**
	 * Creates a text area with empty text content.
	 */
	def this() = {
		this (true)
	}

	def this(text: String) = {
		this()
		appendText(text)
		getUndoManager.forgetHistory()
		getUndoManager.mark()

		// position the caret at the beginning
		selectRange(0, 0)
	}

	private def computeHighlighting(): Unit = {
		val t = getText()
		val charStream = CharStreams.fromReader(new StringReader(t))
		val lexer: GroovyLangLexer = new GroovyLangLexer(charStream)
		val tokensStream: CommonTokenStream = new CommonTokenStream(lexer)
		val spansBuilder = new StyleSpansBuilder[KroovyTokenStyle]()
		try {
			tokensStream.fill()
			import scala.jdk.CollectionConverters._
			val tokens = tokensStream.getTokens.asScala.toList
			spansBuilder.add(new KroovyTokenStyle(0), tokens.headOption.map(_.getStartIndex).getOrElse(t.length))
			for(i <- tokens.indices) {
				val last: Token = if(i > 0) tokens(i-1) else null
				val tkn = tokens(i)
				if(last != null) spansBuilder.add(new KroovyTokenStyle(0), tkn.getStartIndex - (last.getStopIndex+1))
				spansBuilder.add(KroovyTokenStyle(tkn), (tkn.getStopIndex+1)-tkn.getStartIndex)
			}
			spansBuilder.add(new KroovyTokenStyle(0), tokens.lastOption.map(f => t.length - (f.getStopIndex+1)).getOrElse(0))
		}
		catch {
			case exception: LexerNoViableAltException =>
				exception.printStackTrace()
				spansBuilder.add(new KroovyTokenStyle(0), t.length)
		}
		Platform.runLater(() => setStyleSpans(0, spansBuilder.create()))
	}
}