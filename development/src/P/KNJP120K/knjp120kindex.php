<?php

require_once('for_php7.php');
require_once('knjp120kModel.inc');
require_once('knjp120kQuery.inc');

class knjp120kController extends Controller {
    var $ModelClassName = "knjp120kModel";
    var $ProgramID      = "knjp120k";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                case "form2":   //戻る NO002
                    $this->callView("knjp120kForm2");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjp120kForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "right":
                    $args["right_src"] = "knjp120kindex.php?cmd=list";
                    $args["edit_src"] = "knjp120kindex.php?cmd=edit";
                    $args["rows"] = "44%,56%";
                    View::frame($args,"frame3.html");
                    return;
                case "form3":   //異動情報
                    $this->callView("knjp120kForm3");
                    break 2;
                case "form4":   //交付情報
                    $this->callView("knjp120kForm4");
                    break 2;
                case "form5":   //銀行情報
                    $this->callView("knjp120kForm5");
                    break 2;
                case "form6":   //申込情報
                    $this->callView("knjp120kForm6");
                    break 2;
                case "form7":   //軽減情報
                    $this->callView("knjp120kForm7");
                    break 2;
                case "form8":   //費目中分類情報
                    $this->callView("knjp120kForm8");
                    break 2;
                case "form9":   //費目小分類情報
                    $this->callView("knjp120kForm9");
                    break 2;
                case "form10":   //分納情報
                    $this->callView("knjp120kForm10");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/P/KNJP120K/knjp120kindex.php?cmd=right") ."&button=1";
                case "back":
#                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXPK/index.php" .$search;
                    $args["right_src"] = "knjp120kindex.php?cmd=right";
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
$knjp120kCtl = new knjp120kController;
//var_dump($_REQUEST);
?>
