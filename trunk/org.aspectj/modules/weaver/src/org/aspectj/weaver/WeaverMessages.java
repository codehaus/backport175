/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver;

import java.text.MessageFormat;
import java.util.ResourceBundle;


public class WeaverMessages {
	
	private static ResourceBundle bundle = ResourceBundle.getBundle("org.aspectj.weaver.weaver-messages");

	public static final String ARGS_IN_DECLARE = "argsInDeclare";
	public static final String CFLOW_IN_DECLARE = "cflowInDeclare";
	public static final String IF_IN_DECLARE = "ifInDeclare";
	public static final String THIS_OR_TARGET_IN_DECLARE = "thisOrTargetInDeclare";
	public static final String ABSTRACT_POINTCUT = "abstractPointcut";
	public static final String POINCUT_NOT_CONCRETE = "abstractPointcutNotMadeConcrete";
	public static final String CONFLICTING_INHERITED_POINTCUTS = "conflictingInheritedPointcuts";
	public static final String CIRCULAR_POINTCUT = "circularPointcutDeclaration";
	public static final String CANT_FIND_POINTCUT = "cantFindPointcut";
	public static final String EXACT_TYPE_PATTERN_REQD = "exactTypePatternRequired";
	public static final String CANT_BIND_TYPE = "cantBindType";
	public static final String WILDCARD_NOT_ALLOWED = "wildcardTypePatternNotAllowed";
	
	public static final String DECP_OBJECT = "decpObject";
	public static final String CANT_EXTEND_SELF="cantExtendSelf";
	public static final String INTERFACE_CANT_EXTEND_CLASS="interfaceExtendClass";
	public static final String DECP_HIERARCHY_ERROR = "decpHierarchy";
	
	public static final String MULTIPLE_MATCHES_IN_PRECEDENCE = "multipleMatchesInPrecedence";
	public static final String TWO_STARS_IN_PRECEDENCE = "circularityInPrecedenceStar";
	public static final String CLASSES_IN_PRECEDENCE = "nonAspectTypesInPrecedence";
	public static final String TWO_PATTERN_MATCHES_IN_PRECEDENCE = "circularityInPrecedenceTwo";
	
	public static final String NOT_THROWABLE = "notThrowable";
	
	public static final String ITD_CONS_ON_ASPECT = "itdConsOnAspect";
	public static final String ITD_RETURN_TYPE_MISMATCH = "returnTypeMismatch";
	public static final String ITD_PARAM_TYPE_MISMATCH = "paramTypeMismatch";
	public static final String ITD_VISIBILITY_REDUCTION = "visibilityReduction";
	public static final String ITD_DOESNT_THROW = "doesntThrow";
	public static final String ITD_OVERRIDDEN_STATIC = "overriddenStatic";
	public static final String ITD_OVERIDDING_STATIC = "overridingStatic";
	public static final String ITD_CONFLICT = "itdConflict";
	public static final String ITD_MEMBER_CONFLICT = "itdMemberConflict";
	public static final String ITD_NON_EXPOSED_IMPLEMENTOR = "itdNonExposedImplementor";
	public static final String ITD_ABSTRACT_MUST_BE_PUBLIC_ON_INTERFACE = "itdAbstractMustBePublicOnInterface";
	
	public static final String NON_VOID_RETURN = "nonVoidReturn";
	public static final String INCOMPATIBLE_RETURN_TYPE="incompatibleReturnType";
	public static final String CANT_THROW_CHECKED = "cantThrowChecked";
	public static final String CIRCULAR_DEPENDENCY = "circularDependency";
	
	public static final String MISSING_PER_CLAUSE = "missingPerClause";
	public static final String WRONG_PER_CLAUSE = "wrongPerClause";
	
	public static final String ALREADY_WOVEN = "alreadyWoven";
	public static final String REWEAVABLE_MODE = "reweavableMode";
	public static final String PROCESSING_REWEAVABLE = "processingReweavable";
	public static final String MISSING_REWEAVABLE_TYPE = "missingReweavableType";
	public static final String VERIFIED_REWEAVABLE_TYPE = "verifiedReweavableType";
	public static final String ASPECT_NEEDED = "aspectNeeded";
	
	public static final String CANT_FIND_TYPE = "cantFindType";
	public static final String CANT_FIND_CORE_TYPE = "cantFindCoreType";
	public static final String CANT_FIND_TYPE_WITHINPCD = "cantFindTypeWithinpcd";
    public static final String CANT_FIND_TYPE_DURING_AROUND_WEAVE = "cftDuringAroundWeave";
    public static final String CANT_FIND_TYPE_DURING_AROUND_WEAVE_PREINIT = "cftDuringAroundWeavePreinit";
    public static final String CANT_FIND_TYPE_EXCEPTION_TYPE = "cftExceptionType";
    public static final String CANT_FIND_TYPE_ARG_TYPE = "cftArgType";
	
	public static final String DECP_BINARY_LIMITATION = "decpBinaryLimitation";
	public static final String OVERWRITE_JSR45 = "overwriteJSR45";
	public static final String IF_IN_PERCLAUSE = "ifInPerClause";
	public static final String IF_LEXICALLY_IN_CFLOW = "ifLexicallyInCflow";
	public static final String ONLY_BEFORE_ON_HANDLER = "onlyBeforeOnHandler";
	public static final String AROUND_ON_PREINIT = "aroundOnPreInit";
	public static final String AROUND_ON_INIT = "aroundOnInit";
	public static final String AROUND_ON_INTERFACE_STATICINIT = "aroundOnInterfaceStaticInit";
	
	public static final String PROBLEM_GENERATING_METHOD = "problemGeneratingMethod";
	public static final String CLASS_TOO_BIG = "classTooBig";
	
	public static final String ZIPFILE_ENTRY_MISSING = "zipfileEntryMissing";
	public static final String ZIPFILE_ENTRY_INVALID = "zipfileEntryInvalid";
	public static final String DIRECTORY_ENTRY_MISSING = "directoryEntryMissing";
	public static final String OUTJAR_IN_INPUT_PATH = "outjarInInputPath";
	
	
	public static final String XLINT_LOAD_ERROR = "problemLoadingXLint";
	public static final String XLINTDEFAULT_LOAD_ERROR = "unableToLoadXLintDefault";
	public static final String XLINTDEFAULT_LOAD_PROBLEM = "errorLoadingXLintDefault";
	public static final String XLINT_KEY_ERROR = "invalidXLintKey";
	public static final String XLINT_VALUE_ERROR = "invalidXLintMessageKind";
	
	public static final String UNBOUND_FORMAL = "unboundFormalInPC";
	public static final String AMBIGUOUS_BINDING = "ambiguousBindingInPC";
	public static final String AMBIGUOUS_BINDING_IN_OR = "ambiguousBindingInOrPC";
	public static final String NEGATION_DOESNT_ALLOW_BINDING = "negationDoesntAllowBinding";
	
    // Java5 messages
	public static final String ITDC_ON_ENUM_NOT_ALLOWED = "itdcOnEnumNotAllowed";
	public static final String ITDM_ON_ENUM_NOT_ALLOWED = "itdmOnEnumNotAllowed";
	public static final String ITDF_ON_ENUM_NOT_ALLOWED = "itdfOnEnumNotAllowed";
	public static final String CANT_DECP_ON_ENUM_TO_IMPL_INTERFACE = "cantDecpOnEnumToImplInterface";
	public static final String CANT_DECP_ON_ENUM_TO_EXTEND_CLASS = "cantDecpOnEnumToExtendClass";
	public static final String CANT_DECP_TO_MAKE_ENUM_SUPERTYPE = "cantDecpToMakeEnumSupertype";
	public static final String ITDC_ON_ANNOTATION_NOT_ALLOWED = "itdcOnAnnotationNotAllowed";
	public static final String ITDM_ON_ANNOTATION_NOT_ALLOWED = "itdmOnAnnotationNotAllowed";
	public static final String ITDF_ON_ANNOTATION_NOT_ALLOWED = "itdfOnAnnotationNotAllowed";
	public static final String CANT_DECP_ON_ANNOTATION_TO_IMPL_INTERFACE = "cantDecpOnAnnotationToImplInterface";
	public static final String CANT_DECP_ON_ANNOTATION_TO_EXTEND_CLASS = "cantDecpOnAnnotationToExtendClass";
	public static final String CANT_DECP_TO_MAKE_ANNOTATION_SUPERTYPE = "cantDecpToMakeAnnotationSupertype";
	public static final String REFERENCE_TO_NON_ANNOTATION_TYPE = "referenceToNonAnnotationType";
	public static final String BINDING_NON_RUNTIME_RETENTION_ANNOTATION = "bindingNonRuntimeRetentionAnnotation";
	
	public static String format(String key) {
		return bundle.getString(key);
	}
	
	public static String format(String key, Object insert) {
		return MessageFormat.format(bundle.getString(key),new Object[] {insert});
	}

	public static String format(String key, Object insert1, Object insert2) {
		return MessageFormat.format(bundle.getString(key),new Object[] {insert1,insert2});
	}

	public static String format(String key, Object insert1, Object insert2, Object insert3) {
		return MessageFormat.format(bundle.getString(key),new Object[] {insert1, insert2, insert3});
	}

}
