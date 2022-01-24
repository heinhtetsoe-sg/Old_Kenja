<?php

require_once('for_php7.php');

require_once('knja131bModel.inc');
require_once('knja131bQuery.inc');

class knja131bController extends Controller {
    var $ModelClassName = "knja131bModel";
    var $ProgramID      = "KNJA131B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja131b":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja131bModel();		//コントロールマスタの呼び出し
                    $this->callView("knja131bForm1");
                    exit;
                case "clickchange":							//メニュー画面もしくはSUBMITした場合 //NO001
					$sessionInstance->knja131bModel();		//コントロールマスタの呼び出し
                    $this->callView("knja131bForm1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knja131bForm1");
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
$knja131bCtl = new knja131bController;
//var_dump($_REQUEST);
?>
