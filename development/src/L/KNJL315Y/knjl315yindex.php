<?php

require_once('for_php7.php');

require_once('knjl315yModel.inc');
require_once('knjl315yQuery.inc');

class knjl315yController extends Controller {
	var $ModelClassName = "knjl315yModel";
    var $ProgramID      = "KNJL315Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl315y":
					$sessionInstance->knjl315yModel();
					$this->callView("knjl315yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl315yCtl = new knjl315yController;
?>
