<?php

require_once('for_php7.php');

require_once('knjd232Model.inc');
require_once('knjd232Query.inc');

class knjd232Controller extends Controller {
	var $ModelClassName = "knjd232Model";
	var $ProgramID      = "KNJD232";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjd232":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd232Model();		//コントロールマスタの呼び出し
					$this->callView("knjd232Form1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}

	}
}
$knjd232Ctl = new knjd232Controller;
var_dump($_REQUEST);
?>
