<?php

require_once('for_php7.php');
require_once('knjh410Model.inc');
require_once('knjh410Query.inc');

class knjh410Controller extends Controller {
    var $ModelClassName = "knjh410Model";
    var $ProgramID      = "KNJH410";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "radio":
                case "subEnd":
                case "STAFFSORTCLICK":
                case "DATESORTCLICK":
                case "TITLESORTCLICK":
                case "nendo":                           //年度情報
                case "schreg":                          //学籍基礎情報
                case "club":                            //部活情報
                case "committee":                       //委員会情報
                case "shikaku":                         //資格情報
                case "train":                           //指導情報
                case "detail":                          //賞罰情報
                case "attend":                          //出欠情報
                case "tuugaku":                         //通学情報
                case "seito":                           //生徒情報
                case "hogo":                            //保護者情報
                case "hogo2":                           //保護者情報2
                case "hosyou":                          //保証人情報
                case "other":                           //その他情報
                case "family":                          //家族情報
                case "action_document":                 //行動の記録
                    $this->callView("knjh410Form1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "upd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXSEARCH5/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/H/KNJH410/knjh410index.php?cmd=edit")."&button=1";
                    $args["right_src"] = REQUESTROOT ."/X/KNJXSEARCH5/index.php?cmd=right&PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/H/KNJH410/knjh410index.php?cmd=edit")."&button=1";
                    $args["cols"] = "23%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJh400Ctl = new knjh410Controller;
//var_dump($_REQUEST);
?>
