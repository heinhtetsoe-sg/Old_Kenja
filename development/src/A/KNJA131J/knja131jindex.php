<?php

require_once('for_php7.php');

require_once('knja131jModel.inc');
require_once('knja131jQuery.inc');

class knja131jController extends Controller {
    var $ModelClassName = "knja131jModel";
    var $ProgramID      = "KNJA131J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja131j":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja131jModel();		//コントロールマスタの呼び出し
                    $this->callView("knja131jForm1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knja131jForm1");
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
$knja131jCtl = new knja131jController;
//var_dump($_REQUEST);
?>
