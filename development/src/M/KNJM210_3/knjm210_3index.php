<?php

require_once('for_php7.php');

require_once('knjm210_3Model.inc');
require_once('knjm210_3Query.inc');

class knjm210_3Controller extends Controller {
    var $ModelClassName = "knjm210_3Model";
    var $ProgramID      = "KNJM210";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                    $this->callView("knjm210_3Form1");
                    break 2;
                case "edit":
                    $this->callView("knjm210_3Form2");
                    break 2;
                case "edit2":
                    $this->callView("knjm210_3Form3");
                    break 2;
                case "edit3":
                    $this->callView("knjm210_3Form4");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjm210_3index.php?cmd=list&SCHNO={$sessionInstance->schregno}";
                    $args["right_src"] = "knjm210_3index.php?cmd=edit&SCHNO={$sessionInstance->schregno}";
                    $args["cols"] = "10%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm210_3Ctl = new knjm210_3Controller;
?>
