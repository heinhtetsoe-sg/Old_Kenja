<?php

require_once('for_php7.php');

require_once('knjp140kModel.inc');
require_once('knjp140kQuery.inc');

class knjp140kController extends Controller {
    var $ModelClassName = "knjp140kModel";
    var $ProgramID      = "knjp140k";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit1":
                    $sessionInstance->div = "M";
                    $this->callView("knjp140kForm3");
                    break 2;
                case "edit2":
                    $sessionInstance->div = "S";
                    $this->callView("knjp140kForm4");
                    break 2;
                case "list1":
                    $this->callView("knjp140kForm1");
                    break 2;
                case "list2":
                    $this->callView("knjp140kForm2");
                    break 2;
                 //分納対象費目編集の追加と更新
                case "add1":
                case "update1":
                    $sessionInstance->getUpdate1Model();
                    $sessionInstance->setCmd("edit1");
                    break 1;
                //分納の期限と金額編集の追加、更新、削除
                case "add":
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "right":
                    $sessionInstance->div = "M";
                    $args["top_src"] = "knjp140kindex.php?cmd=list1";
                    $args["mid_src"] = "knjp140kindex.php?cmd=list2";
                    $args["bot_src"] = "knjp140kindex.php?cmd=edit1";
                    $args["rows"] = "31%,31%,*";
                    View::frame($args,"frame5.html");
                    return;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/P/KNJP140K/knjp140kindex.php?cmd=right") ."&button=1";
                case "back":
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXPK/index.php" .$search;
                    $args["right_src"] = "knjp140kindex.php?cmd=right";
                    $args["cols"] = "25%,*";
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
$knjp140kCtl = new knjp140kController;
//var_dump($_REQUEST);
?>
