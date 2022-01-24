<?php

require_once('for_php7.php');

require_once('knjl328yModel.inc');
require_once('knjl328yQuery.inc');

class knjl328yController extends Controller {
	var $ModelClassName = "knjl328yModel";
    var $ProgramID      = "KNJL328Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl328y":
					$sessionInstance->knjl328yModel();
					$this->callView("knjl328yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl328yCtl = new knjl328yController;
?>
