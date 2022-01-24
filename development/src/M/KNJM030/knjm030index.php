<?php

require_once('for_php7.php');

require_once('knjm030Model.inc');
require_once('knjm030Query.inc');

class knjm030Controller extends Controller {
    var $ModelClassName = "knjm030Model";
    var $ProgramID      = "KNJM030";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm030":
                    $sessionInstance->knjm030Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm030Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm030Form1");
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
$knjm030Ctl = new knjm030Controller;
//var_dump($_REQUEST);
?>
