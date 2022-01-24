<?php

require_once('for_php7.php');
require_once('knja126mModel.inc');
require_once('knja126mQuery.inc');

class knja126mController extends Controller {
    var $ModelClassName = "knja126mModel";
    var $ProgramID      = "KNJA126M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja126mForm1");
                    break 2;
                case "subform2": //部活動参照
                    $this->callView("knja126mSubForm2");
                    break 2;
                case "subform3": //委員会参照
                    $this->callView("knja126mSubForm3");
                    break 2;
                case "subform1": //資格
                    $this->callView("knja126mSubForm1");
                    break 2;
                case "subform4": //出欠記録参照
                    $this->callView("knja126mSubForm4");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knja126mForm");
                    break 2;
                case "main":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA126M/knja126mindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja126mindex.php?cmd=edit";
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
$knja126mCtl = new knja126mController;
?>
