<?php
require_once('knjl301yModel.inc');
require_once('knjl301yQuery.inc');

class knjl301yController extends Controller {
	var $ModelClassName = "knjl301yModel";
    var $ProgramID      = "KNJL301Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl301y":
					$sessionInstance->knjl301yModel();
					$this->callView("knjl301yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl301yCtl = new knjl301yController;
?>
