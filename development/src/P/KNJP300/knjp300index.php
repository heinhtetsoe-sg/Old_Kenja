<?php

require_once('for_php7.php');

require_once('knjp300Model.inc');
require_once('knjp300Query.inc');

class knjp300Controller extends Controller {
    var $ModelClassName = "knjp300Model";
    var $ProgramID      = "knjp300";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp300":								//メニュー画面もしくはSUBMITした場合
                case "clickcheng":							//メニュー画面もしくはSUBMITした場合
                case "change_class":						//メニュー画面もしくはSUBMITした場合
                case "himoku":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjp300Model();		//コントロールマスタの呼び出し
                    $this->callView("knjp300Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjp300Form1");
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
$knjp300Ctl = new knjp300Controller;
//var_dump($_REQUEST);
?>
