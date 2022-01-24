<?php

require_once('for_php7.php');

require_once('knjm210m_2Model.inc');
require_once('knjm210m_2Query.inc');

class knjm210m_2Controller extends Controller {
    var $ModelClassName = "knjm210m_2Model";
    var $ProgramID      = "KNJM210M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjm210m_2Form1");
                    break 2;
                case "form2":
                    $this->callView("knjm210m_2Form2");
                    break 2;
                case "form3":
                    $this->callView("knjm210m_2Form3");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = REQUESTROOT ."/M/KNJM210M_3/knjm210m_3index.php?SCHNO={$sessionInstance->schregno}&PROGRAMID=" .$this->ProgramID;
                    $args["edit_src"] = "knjm210m_2index.php?cmd=main&SCHNO={$sessionInstance->schregno}";
                    $args["rows"] = "45%,*%";
                    View::frame($args,"frame3.html");
                    return;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm210m_2Ctl = new knjm210m_2Controller;
//var_dump($_REQUEST);
?>
