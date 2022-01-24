<?php

require_once('for_php7.php');
require_once('knje012bModel.inc');
require_once('knje012bQuery.inc');

class knje012bController extends Controller
{
    public $ModelClassName = "knje012bModel";
    public $ProgramID      = "KNJE012B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case 'zentorikomi':
                case "reset":
                case "updEdit":
                case "edit":
                    $this->callView("knje012bForm1");
                    break 2;
                case "syukketsu":   //出欠の記録参照
                    $this->callView("knje012bSyukketsuKirokuSansyo");
                    break 2;
                case "subform2":    //出欠の記録参照
                    $this->callView("knje012bSubForm2");
                    break 2;
                case "shokenlist1":
                case "shokenlist2":
                case "shokenlist3":
                    $this->callView("shokenlist");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/E/KNJE012B/knje012bindex.php?cmd=edit") ."&button=3";
                    $args["right_src"] = "knje012bindex.php?cmd=edit&init=1";
                    $args["cols"] = "20%,*";
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
$knje012bCtl = new knje012bController();
//var_dump($_REQUEST);
