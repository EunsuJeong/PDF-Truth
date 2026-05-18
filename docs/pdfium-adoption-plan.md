# PDF Truth (True's) PDFium Adoption Plan

## 1. Review Purpose
- Define a practical adoption plan for PDFium to enable offline full-text search in PDF Truth.
- Keep current constraints: no dependency/code changes in this phase, documentation-only.

## 2. Current Search Architecture Summary
- UI flow: ViewerScreen -> ViewerViewModel -> PdfSearchRepository -> PdfSearchEngine.
- Current outcome model: PdfSearchOutcome (Success / Empty / Unsupported / Failure).
- Current engine: AndroidPdfRendererSearchEngine returns Unsupported because PdfRenderer cannot extract text.
- Architecture is already pluggable for a future PdfiumSearchEngine.

## 3. Why PDFium Is Needed
- Android PdfRenderer supports page rendering, but not text extraction.
- Full-text search requires:
  - text extraction per page,
  - query matching,
  - page-level result mapping,
  - scalable offline processing.
- PDFium-based stack is the lowest-risk path aligned with offline and free app goals.

## 4. PDFium Candidate Comparison

| Candidate | Android Support | Kotlin/Java Integration | Text Extraction | Page-level Search | License | Maintenance | Size Impact | ABI Scope | Offline | Free App Fit | Integration Difficulty |
|---|---|---|---|---|---|---|---|---|---|---|---|
| barteksc/AndroidPdfViewer lineage (PDFium-backed) | High (widely used) | Medium | Partial/indirect by wrapper version | Possible with adapter work | Apache-2.0 + transitive PDFium notices | Community-maintained forks vary | Medium-High | Usually armeabi-v7a/arm64-v8a/x86/x86_64 | Yes | Good | Medium |
| PdfiumAndroid lineage | High | Medium-High | Yes (depends on exposed APIs) | Yes | Usually Apache-2.0/BSD-compatible chain (verify exact artifact) | Varies by fork/artifact | Medium-High | Multi-ABI native libs | Yes | Good | Medium-High |
| AndroidPdfViewerV2 lineage | High | Medium | Mostly viewer-focused; text APIs may be limited | Possible but not guaranteed by default APIs | Apache-2.0 + transitive notices | Active forks exist, quality varies | Medium-High | Multi-ABI native libs | Yes | Good | Medium |
| Direct PDFium JNI wrapper (in-house) | High (full control) | High complexity | Yes | Yes | BSD-style upstream (with strict notice tracking) | Fully owned burden | Medium-High to High | Full manual ABI handling | Yes | Good | High |
| Other actively maintained PDFium Android wrappers | Medium-High | Medium | Usually yes | Usually yes | Artifact-dependent (must verify) | Unknown until chosen | Medium-High | Artifact-dependent | Yes | Usually good | Medium |

Selection note:
- Prefer a maintained PdfiumAndroid-compatible wrapper exposing stable text APIs with clear license metadata.
- Avoid viewer-only wrappers if text extraction APIs are not reliably exposed.

## 5. License Review
- Baseline requirement: compatible with a fully free app policy (no forced commercial license).
- PDFium upstream is generally BSD-style and permissive.
- Action items for implementation PR:
  - Verify selected artifact LICENSE file and transitive native license notices.
  - Add required notices/attributions in repository docs if needed.
  - Reject candidates with unclear or conflicting redistribution terms.

## 6. Android ABI / Native Impact
- PDFium wrappers include native binaries (*.so) per ABI.
- Expected ABIs: armeabi-v7a, arm64-v8a, x86, x86_64 (artifact-dependent).
- Risks:
  - Increased APK/AAB size,
  - ABI mismatch or missing ABI crashes on some devices/emulators,
  - startup overhead for native loading.
- Mitigation:
  - verify artifact ABI matrix before adoption,
  - use ABI splits if needed in future release optimization PR,
  - test on at least arm64 device + x86_64 emulator.

## 7. App Size Impact
- Native PDFium usually increases package size by several MB.
- Practical risk:
  - larger install/download footprint,
  - slower initial install on low-bandwidth users.
- Mitigation plan:
  - measure baseline vs candidate artifact size in implementation PR,
  - evaluate split APK/AAB strategy later,
  - keep only required ABIs when distribution policy allows.

## 8. Performance / Memory Impact
- Potential gains:
  - fast page-level text search once indexing/query pipeline is tuned.
- Risks:
  - native memory pressure during extraction on large PDFs,
  - slower first query without caching/indexing.
- Mitigation:
  - page-window processing instead of full eager extraction,
  - bounded background jobs and cancellation,
  - result cap per query to avoid UI overload.

## 9. Integration with Existing PdfSearchEngine
- Keep current flow unchanged:
  - ViewerScreen -> ViewerViewModel -> PdfSearchRepository -> PdfSearchEngine -> PdfSearchOutcome.
- Implementation strategy:
  - add PdfiumSearchEngine : PdfSearchEngine,
  - switch DI binding in AppContainer from AndroidPdfRendererSearchEngine to PdfiumSearchEngine (implementation PR only),
  - keep Unsupported fallback path for unsupported/failed initialization.
- Benefit:
  - minimal UI/ViewModel churn due to existing abstraction.

## 10. Expected PdfiumSearchEngine Shape
- Suggested responsibilities:
  - open document with PDFium-backed API,
  - extract text by page,
  - execute query matching,
  - map matches to PdfSearchResult(pageIndex, summary),
  - return PdfSearchOutcome.Success/Empty/Failure.
- Suggested helpers (future):
  - PdfiumDocumentSessionAdapter,
  - PdfTextExtractor,
  - PdfSearchMatcher.

## 11. Fallback Strategy
- If PDFium init fails or artifact is unstable:
  - return PdfSearchOutcome.Unsupported with explicit user-safe message,
  - preserve current viewer behavior (rendering/navigation unaffected),
  - log only non-sensitive diagnostics (no tracking, no cloud).
- If extraction partially fails on specific files:
  - return PdfSearchOutcome.Failure with concise reason,
  - allow user to continue reading normally.

## 12. Implementation PR Task List (Future)
1. Select final PDFium artifact/fork with clear maintenance and license clarity.
2. Add dependency and required native packaging settings.
3. Implement PdfiumSearchEngine.
4. Wire DI binding in AppContainer.
5. Add integration tests for Success/Empty/Unsupported/Failure outcomes.
6. Validate behavior on large PDFs and multilingual text.
7. Validate ABI/device coverage (arm64 + emulator).
8. Add/update license notice documentation.
9. Measure and report APK/AAB size delta.
10. Prepare rollback toggle or quick revert plan.

## 13. Risks and Mitigations
- Risk: artifact maintenance stagnation.
  - Mitigation: choose active fork with recent releases/issues activity.
- Risk: native crashes on specific ABIs.
  - Mitigation: ABI verification matrix and smoke tests.
- Risk: size increase beyond acceptable threshold.
  - Mitigation: measure early, apply ABI split optimization in follow-up.
- Risk: legal ambiguity in transitive components.
  - Mitigation: explicit license audit before merge.

## 14. Final Recommendation
- Confirm PDFium as the primary engine direction.
- Prefer a maintained PdfiumAndroid-compatible wrapper with explicit text extraction APIs and clear permissive licensing metadata.
- Keep current abstraction unchanged and deliver adoption in a separate implementation PR.
- Retain Unsupported fallback path to protect user experience if native engine init/search fails.

## Deferred in This PR
- No Gradle changes.
- No dependency additions.
- No JNI/native code.
- No PdfiumSearchEngine implementation.
- No UI/Room/DataStore/Viewer structure changes.
