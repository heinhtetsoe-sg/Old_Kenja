<?php

require_once('for_php7.php');

require_once('knjl040oModel.inc');
require_once('knjl040oQuery.inc');

class knjl040oController extends Controller {
    var $ModelClassName = "knjl040oModel";
    var $ProgramID      = "KNJL040O";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl040oForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    if ($sessionInstance->warning) {
                       echo "<script language=\"javascript\"> alert('".$sessionInstance->warning."');</script>";
                    } elseif ($sessionInstance->message) {
                       echo "<script language=\"javascript\"> alert('".$sessionInstance->message."');";
                       echo "top.main_frame.bottom_frame.location.href = '".REQUESTROOT."/L/KNJL040O/knjl040oindex.php?cmd=read';";
                       echo "</script>";
                    }
                    unset($sessionInstance->warning);
                    unset($sessionInstance->message);
                    
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = "";
                    $args["edit_src"]  = "knjl040oindex.php?cmd=main";
                    $args["rows"] = "0%,*";
                    View::frame($args, "frame3.html",0);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl040oCtl = new knjl040oController;
?>
