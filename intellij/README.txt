If you are trying to create an IntelliJ project to hack that plugin, you d better run with "Irida" ie the EAP version
from IntelliJ v5.

The builded plugin can work on v4.5 as well.

building:
set IDEA_HOME
ant clean dist

instal:
ant install
[need to close IDEA]

troubleshooting:
end of IDEA_HOME/bin/log.xml:
(+ idea.lax stdout redirect)
	<category name="org.codehaus.backport175">
       		<priority value="DEBUG"/>
       		<appender-ref ref="FILE"/>
	</category>
