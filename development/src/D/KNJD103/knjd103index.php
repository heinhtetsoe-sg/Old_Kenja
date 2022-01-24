<?php

require_once('for_php7.php');

require_once('knjd103Model.inc');
require_once('knjd103Query.inc');

class knjd103Controller extends Controller {
    var $ModelClassName = "knjd103Model";
    var $ProgramID      = "KNJD103";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd103":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd103Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd103Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjd103Form1");
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
$knjd103Ctl = new knjd103Controller;
//var_dump($_REQUEST);
?>
