<?php

require_once('for_php7.php');

require_once('knjh311Model.inc');
require_once('knjh311Query.inc');

class knjh311Controller extends Controller {
    var $ModelClassName = "knjh311Model";
    var $ProgramID      = "KNJH311";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh311":
                case "changetarget":
                    //分割フレーム作成
                    $sessionInstance->knjh311Model();
                    $this->callView("knjh311Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh311Ctl = new knjh311Controller;
?>
