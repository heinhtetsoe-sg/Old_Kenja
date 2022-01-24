<?php

require_once('for_php7.php');

require_once('knjf120Model.inc');
require_once('knjf120Query.inc');

class knjf120Controller extends Controller {
    var $ModelClassName = "knjf120Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf120":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjf120Model();		//コントロールマスタの呼び出し
                    $this->callView("knjf120Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjf120Form1");
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
$knjf120Ctl = new knjf120Controller;
//var_dump($_REQUEST);
?>
