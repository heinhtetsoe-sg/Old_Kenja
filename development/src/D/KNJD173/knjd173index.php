<?php

require_once('for_php7.php');

require_once('knjd173Model.inc');
require_once('knjd173Query.inc');

class knjd173Controller extends Controller {
    var $ModelClassName = "knjd173Model";
    var $ProgramID      = "KNJD173";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd173":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd173Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd173Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjd173Form1");
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
$knjd173Ctl = new knjd173Controller;
//var_dump($_REQUEST);
?>
