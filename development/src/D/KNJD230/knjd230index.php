<?php

require_once('for_php7.php');

require_once('knjd230Model.inc');
require_once('knjd230Query.inc');

class knjd230Controller extends Controller {
    var $ModelClassName = "knjd230Model";
    var $ProgramID      = "KNJD230";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd230":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd230Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd230Form1");
                    exit;
				case "csv":     //CSVダウンロード//2004/07/07 add nakamoto//
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjd230Form1");
					}
					break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd230Ctl = new knjd230Controller;
var_dump($_REQUEST);
?>
