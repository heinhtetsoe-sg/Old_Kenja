<?php

require_once("for_php7.php");

require_once('knja125jModel.inc');
require_once('knja125jQuery.inc');

class knja125jController extends Controller
{
    public $ModelClassName = "knja125jModel";
    public $ProgramID      = "KNJA125J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "torikomi3":
                case "value_set":
                case "edit":
                case "clear":
                case "reload2":
                case "reload_doutoku":
                case "zentorikomi":
                    $this->callView("knja125jForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "form2":
                case "form2_torikomi":
                case "clear2":
                    $this->callView("knja125jForm2");
                    break 2;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "subform1":    //通知表所見参照
                    $this->callView("knja125jSubForm1");
                    break 2;
                case "subform2":    //出欠の記録参照
                    $this->callView("knja125jSubForm2");
                    break 2;
                case "subform3":    //通知票の参照(道徳)※近大中学のみ表示
                    $this->callView("knja125jSubForm3");
                    break 2;
                case "subform4":    //調査書特別活動参照
                    $this->callView("knja125jSubForm4");
                    break 2;
                case "syukketsu":   //出欠の記録参照
                    $this->callView("knja125jSyukketsuKirokuSansyo");
                    break 2;
                case "act_doc":     //行動の記録参照
                    $this->callView("knja125jActDoc");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "shokenlist1":
                case "shokenlist2":
                case "shokenlist3":
                case "shokenlist4":
                    $this->callView("shokenlist");
                    break 2;
                case "teikei":
                case "teikei_act":
                case "teikei_val":
                    $this->callView("knja125jSubMaster");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA125J/knja125jindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1";
                    $args["right_src"] = "knja125jindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
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
$knja125jCtl = new knja125jController();
