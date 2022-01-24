<?php

require_once('for_php7.php');

require_once('knjd231Model.inc');
require_once('knjd231Query.inc');

class knjd231Controller extends Controller {
    var $ModelClassName = "knjd231Model";
    var $ProgramID      = "KNJD231";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd231":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd231Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd231Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjd231Form1");
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
$knjd231Ctl = new knjd231Controller;
//var_dump($_REQUEST);
?>
