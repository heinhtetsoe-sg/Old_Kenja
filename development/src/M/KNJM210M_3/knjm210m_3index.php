<?php

require_once('for_php7.php');

require_once('knjm210m_3Model.inc');
require_once('knjm210m_3Query.inc');

class knjm210m_3Controller extends Controller {
    var $ModelClassName = "knjm210m_3Model";
    var $ProgramID      = "KNJM210m";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                    $this->callView("knjm210m_3Form1");
                    break 2;
                case "edit":
                    $this->callView("knjm210m_3Form2");
                    break 2;
                case "edit2":
                    $this->callView("knjm210m_3Form3");
                    break 2;
                case "edit3":
                    $this->callView("knjm210m_3Form4");
                    break 2;
                case "edit4":
                    $this->callView("knjm210m_3Form5");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjm210m_3index.php?cmd=list&SCHNO={$sessionInstance->schregno}";
                    $args["right_src"] = "knjm210m_3index.php?cmd=edit&SCHNO={$sessionInstance->schregno}";
                    $args["cols"] = "12%,*%";
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
$knjm210m_3Ctl = new knjm210m_3Controller;
?>
