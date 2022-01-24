<?php

require_once('for_php7.php');

require_once('knjm060Model.inc');
require_once('knjm060Query.inc');

class knjm060Controller extends Controller {
    var $ModelClassName = "knjm060Model";
    var $ProgramID      = "KNJM060";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm060":
                    $sessionInstance->knjm060Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm060Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm060Form1");
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
$knjm060Ctl = new knjm060Controller;
//var_dump($_REQUEST);
?>
