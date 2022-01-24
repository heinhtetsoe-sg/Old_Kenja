<?php

require_once('for_php7.php');

require_once('knje131Model.inc');
require_once('knje131Query.inc');

class knje131Controller extends Controller {
    var $ModelClassName = "knje131Model";
    var $ProgramID      = "KNJE131";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje131":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knje131Model();		//コントロールマスタの呼び出し
                    $this->callView("knje131Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knje131Form1");
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
$knje131Ctl = new knje131Controller;
//var_dump($_REQUEST);
?>
