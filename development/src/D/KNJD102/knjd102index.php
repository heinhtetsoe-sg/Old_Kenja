<?php

require_once('for_php7.php');

require_once('knjd102Model.inc');
require_once('knjd102Query.inc');

class knjd102Controller extends Controller {
    var $ModelClassName = "knjd102Model";
    var $ProgramID      = "KNJD102";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd102":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd102Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd102Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjd102Form1");
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
$knjd102Ctl = new knjd102Controller;
//var_dump($_REQUEST);
?>
