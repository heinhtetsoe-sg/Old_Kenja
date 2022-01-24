<?php

require_once('for_php7.php');

require_once('knjd645Model.inc');
require_once('knjd645Query.inc');

class knjd645Controller extends Controller {
    var $ModelClassName = "knjd645Model";
    var $ProgramID      = "KNJD645";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd645":
                    $sessionInstance->knjd645Model();
                    $this->callView("knjd645Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd645Form1");
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
$knjd645Ctl = new knjd645Controller;
//var_dump($_REQUEST);
?>
