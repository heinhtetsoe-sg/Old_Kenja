<?php
require_once('knjl325yModel.inc');
require_once('knjl325yQuery.inc');

class knjl325yController extends Controller {
	var $ModelClassName = "knjl325yModel";
    var $ProgramID      = "KNJL325Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl325y":
					$sessionInstance->knjl325yModel();
					$this->callView("knjl325yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl325yCtl = new knjl325yController;
?>
