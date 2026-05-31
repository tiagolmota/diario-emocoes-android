# proguard-rules.pro
# Regras ProGuard para o projecto DiarioEmocoes.
# Aplicadas apenas em builds de release (minifyEnabled true).

# Manter entidades Room (o compilador Room precisa de aceder aos nomes em runtime)
-keep class pt.isla.diarioemocoes.data.** { *; }

# Manter anotações do Room (necessárias para o processador de anotações)
-keepattributes *Annotation*

# Manter getters/setters dos POJOs (Room usa-os para ler/escrever campos)
-keepclassmembers class pt.isla.diarioemocoes.data.RegistoEmocao {
    public <init>(...);
    public *** get*();
    public void set*(***);
}

# Suprimir avisos de bibliotecas internas do Android
-dontwarn android.databinding.**
