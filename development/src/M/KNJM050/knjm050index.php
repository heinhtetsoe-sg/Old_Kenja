<?php

require_once('for_php7.php');

require_once('knjm050Model.inc');
require_once('knjm050Query.inc');

class knjm050Controller extends Controller {
    var $ModelClassName = "knjm050Model";
    var $ProgramID      = "KNJM050";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm050":
                    $sessionInstance->knjm050Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm050Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm050Form1");
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
$knjm050Ctl = new knjm050Controller;
//var_dump($_REQUEST);
?>
