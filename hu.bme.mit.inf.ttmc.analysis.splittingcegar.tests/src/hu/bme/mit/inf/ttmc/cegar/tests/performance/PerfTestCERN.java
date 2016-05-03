package hu.bme.mit.inf.ttmc.cegar.tests.performance;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import hu.bme.mit.inf.ttmc.cegar.common.CEGARBuilder;
import hu.bme.mit.inf.ttmc.cegar.interpolatingcegar.InterpolatingCEGARBuilder;
import hu.bme.mit.inf.ttmc.cegar.interpolatingcegar.InterpolatingCEGARBuilder.InterpolationMethod;
import hu.bme.mit.inf.ttmc.cegar.tests.formatters.Formatter;
import hu.bme.mit.inf.ttmc.cegar.tests.formatters.impl.ExcelFormatter;
import hu.bme.mit.inf.ttmc.cegar.visiblecegar.VisibleCEGARBuilder;
import hu.bme.mit.inf.ttmc.cegar.visiblecegar.VisibleCEGARBuilder.VarCollectionMethod;

public class PerfTestCERN extends PerfTestBase {

	private static final int TIMEOUT = 5 * 60 * 1000;
	private static final Formatter FORMATTER = new ExcelFormatter();

	@SuppressWarnings("serial")

	@Test
	public void testCERNPLC() {
		final IModelLoader loader = new SystemFileModelLoader();

		final List<TestCase> testCases = new ArrayList<TestCase>() {
			{
				add(new TestCase("models/cern/LOCAL_vc1.system", false, loader));
				add(new TestCase("models/cern/LOCAL_vc2.system", false, loader));
				add(new TestCase("models/cern/REQ_1-1.system", true, loader));
				add(new TestCase("models/cern/REQ_1-8_correct.system", true, loader));
				add(new TestCase("models/cern/REQ_1-8_incorrect.system", false, loader));
				add(new TestCase("models/cern/REQ_1-9.system", true, loader));
				add(new TestCase("models/cern/REQ_2-3b_correct.system", true, loader));
				add(new TestCase("models/cern/REQ_2-3b_incorrect.system", false, loader));
				add(new TestCase("models/cern/REQ_3-1.system", true, loader));
				add(new TestCase("models/cern/REQ_3-2.system", false, loader));
				add(new TestCase("models/cern/UCPC-1721.system", true, loader));
			}
		};
		final List<CEGARBuilder> configurations = new ArrayList<CEGARBuilder>() {
			{
				add(new VisibleCEGARBuilder().logger(null).visualizer(null).useCNFTransformation(false)
						.varCollectionMethod(VarCollectionMethod.CraigItp));
				add(new VisibleCEGARBuilder().logger(null).visualizer(null).useCNFTransformation(false)
						.varCollectionMethod(VarCollectionMethod.SequenceItp));
				add(new InterpolatingCEGARBuilder().logger(null).visualizer(null).interpolationMethod(InterpolationMethod.Craig).incrementalModelChecking(true)
						.useCNFTransformation(false));
				add(new InterpolatingCEGARBuilder().logger(null).visualizer(null).interpolationMethod(InterpolationMethod.Craig).incrementalModelChecking(true)
						.useCNFTransformation(false).explicitVar("loc"));
				add(new InterpolatingCEGARBuilder().logger(null).visualizer(null).interpolationMethod(InterpolationMethod.Sequence)
						.incrementalModelChecking(true).useCNFTransformation(false));
				add(new InterpolatingCEGARBuilder().logger(null).visualizer(null).interpolationMethod(InterpolationMethod.Sequence)
						.incrementalModelChecking(true).useCNFTransformation(false).explicitVar("loc"));
			}
		};

		run(testCases, configurations, TIMEOUT, FORMATTER);
	}

}