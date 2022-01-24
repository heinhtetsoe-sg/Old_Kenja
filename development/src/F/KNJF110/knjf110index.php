<?php

require_once('for_php7.php');

require_once('knjf110Model.inc');
require_once('knjf110Query.inc');

class knjf110Controller extends Controller {
    var $ModelClassName = "knjf110Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf110":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjf110Model();		//コントロールマスタの呼び出し
                    $this->callView("knjf110Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjf110Form1");
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
$knjf110Ctl = new knjf110Controller;
//var_dump($_REQUEST);
?>
