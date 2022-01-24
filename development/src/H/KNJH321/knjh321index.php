<?php

require_once('for_php7.php');

require_once('knjh321Model.inc');
require_once('knjh321Query.inc');

class knjh321Controller extends Controller {
    var $ModelClassName = "knjh321Model";
    var $ProgramID      = "KNJH321";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh321":
                case "changetarget":
                    //分割フレーム作成
                    $sessionInstance->knjh321Model();
                    $this->callView("knjh321Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh321Ctl = new knjh321Controller;
?>
