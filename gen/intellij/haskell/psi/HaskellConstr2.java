// This is a generated file. Not intended for manual editing.
package intellij.haskell.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HaskellConstr2 extends HaskellCompositeElement {

    @NotNull
    List<HaskellPragma> getPragmaList();

    @NotNull
    List<HaskellQName> getQNameList();

    @NotNull
    List<HaskellTtype> getTtypeList();

    @NotNull
    List<HaskellTypeSignature> getTypeSignatureList();

}
