<?php
require_once('knjl350yModel.inc');
require_once('knjl350yQuery.inc');

class knjl350yController extends Controller {
	var $ModelClassName = "knjl350yModel";
    var $ProgramID      = "KNJL350Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl350y":
					$sessionInstance->knjl350yModel();
					$this->callView("knjl350yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl350yCtl = new knjl350yController;
?>
