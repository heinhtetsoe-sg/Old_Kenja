<?php

require_once('for_php7.php');
require_once('knjj090Model.inc');
require_once('knjj090Query.inc');

class knjj090Controller extends Controller {
    var $ModelClassName = "knjj090Model";
    var $ProgramID      = "KNJJ090";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("knjj090Form1");
                    break 2;
                case "edit":
                    $this->callView("knjj090Form2");
                    break 2;
                case "right_list":
				case "from_list":
                    $this->callView("knjj090Form1");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjj090Model();	//2004/05/12 nakamoto add
					
                case "":
                    $sessionInstance->knjj090Model();	//2004/05/12 nakamoto add
					
                    //分割フレーム作成
					
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/J/KNJJ090/knjj090index.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjj090index.php?cmd=right_list";
					$args["edit_src"]  = "knjj090index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
					$args["rows"] = "50%,50%";
                    View::frame($args,"frame2.html");
                    return;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj090Ctl = new knjj090Controller;
?>
