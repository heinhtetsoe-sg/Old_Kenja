<?php

require_once('for_php7.php');

require_once('knjd649Model.inc');
require_once('knjd649Query.inc');

class knjd649Controller extends Controller {
    var $ModelClassName = "knjd649Model";
    var $ProgramID      = "KNJD649";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd649":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd649Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd649Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjd649Form1");
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
$knjd649Ctl = new knjd649Controller;
//var_dump($_REQUEST);
?>
