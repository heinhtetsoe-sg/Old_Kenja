<?php

require_once('for_php7.php');

require_once('knjd173aModel.inc');
require_once('knjd173aQuery.inc');

class knjd173aController extends Controller {
    var $ModelClassName = "knjd173aModel";
    var $ProgramID      = "KNJD173A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd173a":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd173aModel();		//コントロールマスタの呼び出し
                    $this->callView("knjd173aForm1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjd173aForm1");
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
$knjd173aCtl = new knjd173aController;
//var_dump($_REQUEST);
?>
