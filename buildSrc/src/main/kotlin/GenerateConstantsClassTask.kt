import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier
import lombok.NoArgsConstructor
import net.fabricmc.api.Environment
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*

abstract class GenerateConstantsClassTask : GenerateSourceTask() {
    @Input
    lateinit var constants: Map<String, Any?>
    @Input
    var className: String = "Constants"

    @TaskAction
    @Throws(Exception::class)
    override fun generate() {
        val typeSpec: TypeSpec.Builder =
            TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        var out: JavaFile = JavaFile.builder(packageTarget, typeSpec.build()).build()
        out.toJavaFileObject().delete()

        val lombok: AnnotationSpec =
            AnnotationSpec.builder(NoArgsConstructor::class.java)
                .addMember("access", CodeBlock.builder().add("\$L", "lombok.AccessLevel.PRIVATE").build()).build()
        val fabric: AnnotationSpec =
            AnnotationSpec.builder(Environment::class.java)
                .addMember("value", CodeBlock.builder().add("\$L", "net.fabricmc.api.EnvType.CLIENT").build()).build()

        typeSpec.addAnnotation(lombok).addAnnotation(fabric)

        constants.forEach { (key, value) ->
            val k0 = key.uppercase(Locale.getDefault()).replace("[\\.\\-]+", "_")
            typeSpec.addField(
                FieldSpec.builder(String::class.java, k0)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\"$value\"").build()
            )
        }

        typeSpec.addJavadoc("Do not modify this file manually!")

        out = JavaFile.builder(packageTarget, typeSpec.build()).build()
        out.writeTo(File(outputDir).toPath())
    }
}